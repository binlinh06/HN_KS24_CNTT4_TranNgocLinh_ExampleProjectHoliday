'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { PageHeader } from '@/components/ui/page-header';
import {
  useOrderDetails,
  useOrderTrackingQuery,
  useOrderReview,
  useCreateReview,
} from '@/features/orders/api';
import { useOrderTrackingSSE } from '@/features/orders/hooks/useOrderTrackingSSE';
import { StatusBadge } from '@/components/ui/status-badge';
import { LoadingSpinner } from '@/components/ui/loading-spinner';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import toast from 'react-hot-toast';
import {
  CheckCircle2,
  Clock,
  Truck,
  CookingPot,
  Utensils,
  XCircle,
  MapPin,
  Phone,
  User,
  FileText,
  DollarSign,
  ArrowLeft,
  Star,
  Sparkles,
  Wifi,
  WifiOff,
} from 'lucide-react';

const reviewSchema = z.object({
  rating: z.number().min(1, 'Vui lòng chọn số sao đánh giá').max(5),
  comment: z.string().max(1000, 'Đánh giá tối đa 1000 ký tự').optional(),
});

type ReviewFormValues = z.infer<typeof reviewSchema>;

export default function OrderDetailsAndTrackingPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();
  const orderId = params.id as string;
  const writeReviewRequested = searchParams.get('writeReview') === 'true';

  // 1. Fetch REST Order Details (Initial Snapshot & Basic info)
  const { data: order, isLoading: detailsLoading, error: detailsError, refetch: refetchDetails } = useOrderDetails(orderId);

  // 2. Fetch REST Order Tracking Timeline (for loading page, refresh or disconnect fallback)
  const { data: tracking, isLoading: trackingLoading, refetch: refetchTracking } = useOrderTrackingQuery(orderId);

  // 3. Real-time Status Updates via Server-Sent Events
  const { isConnected, error: sseError } = useOrderTrackingSSE(orderId);

  // 4. Fetch existing review if any
  const { data: existingReview, refetch: refetchReview } = useOrderReview(orderId);

  // 5. Submit review mutation
  const createReviewMutation = useCreateReview(orderId);

  // Star rating hover state for review form
  const [hoverRating, setHoverRating] = useState<number | null>(null);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
    reset,
  } = useForm<ReviewFormValues>({
    resolver: zodResolver(reviewSchema),
    defaultValues: {
      rating: 5,
      comment: '',
    },
  });

  const rating = watch('rating');
  const commentValue = watch('comment') || '';

  const onSubmitReview = async (values: ReviewFormValues) => {
    try {
      await createReviewMutation.mutateAsync({
        rating: values.rating,
        comment: values.comment,
      });
      toast.success('Gửi đánh giá thành công! Cảm ơn bạn.');
      reset();
      refetchDetails();
      refetchReview();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra khi gửi đánh giá');
    }
  };

  // Scroll to review form if writeReview=true in URL query
  useEffect(() => {
    if (writeReviewRequested && order?.status === 'HOAN_THANH') {
      const element = document.getElementById('review-section');
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }, [writeReviewRequested, order]);

  if (detailsLoading || trackingLoading) {
    return (
      <div className="flex justify-center items-center py-32 max-w-6xl mx-auto">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (detailsError || !order) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-12 text-center space-y-4">
        <div className="bg-red-50 text-red-700 p-6 rounded-xl border border-red-100 max-w-lg mx-auto">
          Không tìm thấy đơn hàng hoặc bạn không có quyền xem thông tin đơn hàng này.
        </div>
        <Button onClick={() => router.push('/customer/orders')} className="bg-[#0F6B4F]">
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  const getTimelineSteps = () => {
    const defaultSteps: { status: any; label: string; icon: any; desc: string }[] = [
      { status: 'CHO_XAC_NHAN', label: 'Chờ xác nhận', icon: Clock, desc: 'Đơn hàng đang chờ nhà hàng tiếp nhận' },
      { status: 'DA_XAC_NHAN', label: 'Đã xác nhận', icon: CheckCircle2, desc: 'Nhà hàng đã xác nhận đơn hàng' },
      { status: 'DANG_CHE_BIEN', label: 'Đang chế biến', icon: CookingPot, desc: 'Đầu bếp đang chuẩn bị phở bò nóng hổi' },
      {
        status: order.orderType === 'ONLINE' ? 'DANG_GIAO' : 'DANG_PHUC_VU',
        label: order.orderType === 'ONLINE' ? 'Đang giao hàng' : 'Đang phục vụ',
        icon: order.orderType === 'ONLINE' ? Truck : Utensils,
        desc: order.orderType === 'ONLINE' ? 'Shipper đang giao đơn hàng đến bạn' : 'Món ăn đang được mang ra bàn',
      },
      { status: 'HOAN_THANH', label: 'Hoàn thành', icon: CheckCircle2, desc: 'Đơn hàng đã được phục vụ/giao thành công' },
    ];

    if (order.status === 'DA_HUY') {
      // If cancelled, insert DA_HUY at the end
      defaultSteps.push({
        status: 'DA_HUY',
        label: 'Đã hủy',
        icon: XCircle,
        desc: 'Đơn hàng đã bị hủy bỏ',
      });
    }

    return defaultSteps;
  };

  const timelineSteps = getTimelineSteps();
  const timelineHistoryMap = new Map(tracking?.timeline?.map((t) => [t.status, t]));

  // Index of current active step in our stepper
  const currentStepIndex = timelineSteps.findIndex((s) => s.status === order.status);

  return (
    <div className="space-y-8 max-w-6xl mx-auto px-4 sm:px-6 pb-12">
      {/* Navigation Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <Link
          href="/customer/orders"
          className="inline-flex items-center gap-1.5 text-sm font-semibold text-gray-600 hover:text-[#0F6B4F] transition"
        >
          <ArrowLeft className="h-4 w-4" />
          Quay lại danh sách đơn hàng
        </Link>

        {/* Real-time Status Badge Indicator */}
        <div className="flex items-center gap-2 text-xs bg-gray-50 border border-gray-100 rounded-full px-3 py-1.5 shadow-sm w-fit">
          {isConnected ? (
            <>
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
              </span>
              <span className="font-semibold text-emerald-700 flex items-center gap-1">
                <Wifi className="h-3.5 w-3.5" /> Theo dõi thời gian thực
              </span>
            </>
          ) : (
            <>
              <span className="h-2 w-2 rounded-full bg-gray-400"></span>
              <span className="font-semibold text-gray-500 flex items-center gap-1">
                <WifiOff className="h-3.5 w-3.5" /> Chế độ tĩnh (Tải lại sau 15s)
              </span>
            </>
          )}
        </div>
      </div>

      <PageHeader
        title={`Chi tiết đơn hàng #${order.orderCode}`}
        description={`Đặt ngày ${formatDate(order.createdAt)} • Trạng thái hiện tại: ${order.status}`}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left column: Timeline tracker & snapshot items */}
        <div className="lg:col-span-2 space-y-8">
          {/* Real-time Progress Tracking Stepper */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 sm:p-8">
            <h3 className="text-base font-bold text-gray-900 mb-6 flex items-center gap-2">
              <Sparkles className="h-5 w-5 text-[#C7A45B]" /> Theo dõi tiến độ chuẩn bị
            </h3>

            {/* Stepper container */}
            <div className="relative pl-6 sm:pl-8 border-l border-gray-100 space-y-8 py-2">
              {timelineSteps.map((step, idx) => {
                const stepHistory = timelineHistoryMap.get(step.status);
                const isPassed = timelineHistoryMap.has(step.status) || idx <= currentStepIndex;
                const isCurrent = step.status === order.status;
                const StepIcon = step.icon;

                return (
                  <div key={step.status} className="relative">
                    {/* Stepper circle icon */}
                    <div
                      className={`absolute -left-[38px] sm:-left-[46px] top-0.5 w-8 h-8 rounded-full flex items-center justify-center border transition duration-300 z-10 ${
                        isCurrent
                          ? 'bg-[#0F6B4F] border-[#0F6B4F] text-white shadow-md'
                          : isPassed
                          ? 'bg-emerald-50 border-emerald-200 text-emerald-600'
                          : 'bg-white border-gray-200 text-gray-400'
                      }`}
                    >
                      <StepIcon className="h-4 w-4" />
                    </div>

                    {/* Stepper info */}
                    <div className="space-y-1">
                      <div className="flex flex-wrap items-baseline gap-2">
                        <h4
                          className={`text-sm font-bold transition duration-300 ${
                            isCurrent
                              ? 'text-[#0F6B4F] text-base'
                              : isPassed
                              ? 'text-gray-800'
                              : 'text-gray-400'
                          }`}
                        >
                          {step.label}
                        </h4>
                        {stepHistory && (
                          <span className="text-xs text-gray-400 font-medium">
                            {formatDate(stepHistory.changedAt)}
                          </span>
                        )}
                      </div>
                      <p className={`text-xs ${isCurrent ? 'text-gray-600 font-medium' : 'text-gray-400'}`}>
                        {stepHistory?.reason || step.desc}
                      </p>
                      {stepHistory?.changedByRole && stepHistory.changedByRole !== 'SYSTEM' && (
                        <div className="text-[10px] text-gray-400 mt-1 font-mono">
                          Người cập nhật: {stepHistory.changedByRole === 'CUSTOMER' ? 'Khách hàng' : 'Nhân viên cửa hàng'}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Snapshot Items table */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex items-center justify-between">
              <h3 className="text-sm font-bold text-gray-900 uppercase tracking-wider">
                Món ăn đã đặt
              </h3>
              <span className="text-xs text-gray-400 font-mono">
                {order.items?.length || 0} món
              </span>
            </div>

            <div className="divide-y divide-gray-100">
              {order.items?.map((item) => (
                <div key={item.id} className="p-6 space-y-2">
                  <div className="flex justify-between items-start gap-4">
                    <div>
                      <h4 className="text-sm font-bold text-gray-900">
                        {item.productNameSnapshot}
                      </h4>
                      {item.specialNote && (
                        <p className="text-xs text-amber-600 bg-amber-50 rounded px-2 py-0.5 mt-1.5 w-fit">
                          Ghi chú: {item.specialNote}
                        </p>
                      )}
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-gray-400">
                        {item.quantity} x {formatPrice(item.unitPriceSnapshot)}
                      </div>
                      <div className="text-sm font-bold text-gray-800">
                        {formatPrice(item.lineTotal)}
                      </div>
                    </div>
                  </div>

                  {/* Options snapshots list */}
                  {item.options && item.options.length > 0 && (
                    <div className="bg-gray-50 rounded-lg p-2.5 text-xs text-gray-500 space-y-1">
                      <span className="font-semibold text-gray-600 block mb-1">
                        Tuỳ chọn đính kèm:
                      </span>
                      {item.options.map((opt) => (
                        <div key={opt.id} className="flex justify-between items-center pl-2 border-l border-gray-200">
                          <span>
                            {opt.optionGroupNameSnapshot}: {opt.optionNameSnapshot}
                          </span>
                          {opt.incrementalPriceSnapshot > 0 && (
                            <span className="text-gray-400 font-mono">
                              +{formatPrice(opt.incrementalPriceSnapshot)}
                            </span>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>

            {/* Calculations summaries */}
            <div className="p-6 bg-gray-50 border-t border-gray-100 flex justify-end">
              <div className="w-full sm:w-80 space-y-2 text-sm text-gray-600">
                <div className="flex justify-between">
                  <span>Tạm tính:</span>
                  <span className="font-medium text-gray-800">{formatPrice(order.subtotal)}</span>
                </div>
                {order.shippingFee && order.shippingFee > 0 ? (
                  <div className="flex justify-between">
                    <span>Phí vận chuyển:</span>
                    <span className="font-medium text-gray-800">+{formatPrice(order.shippingFee)}</span>
                  </div>
                ) : null}
                {order.discountAmount && order.discountAmount > 0 ? (
                  <div className="flex justify-between text-emerald-600">
                    <span className="flex items-center gap-1">
                      Giảm giá {order.voucherCodeSnapshot && `(${order.voucherCodeSnapshot})`}:
                    </span>
                    <span className="font-bold">-{formatPrice(order.discountAmount)}</span>
                  </div>
                ) : null}
                <div className="h-px bg-gray-200 my-2"></div>
                <div className="flex justify-between text-base font-bold text-gray-900">
                  <span>Tổng thanh toán:</span>
                  <span className="text-lg text-[#0F6B4F]">{formatPrice(order.finalAmount)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right column: Customer info & payment details & review form */}
        <div className="space-y-8">
          {/* Customer / Shipping info */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 space-y-4">
            <h3 className="text-sm font-bold text-gray-900 uppercase tracking-wider border-b border-gray-100 pb-3">
              Thông tin nhận hàng
            </h3>

            {order.orderType === 'ONLINE' ? (
              <div className="space-y-3.5 text-sm text-gray-600">
                <div className="flex items-start gap-2.5">
                  <User className="h-4.5 w-4.5 text-gray-400 mt-0.5 flex-shrink-0" />
                  <div>
                    <div className="font-bold text-gray-800">{order.receiverNameSnapshot}</div>
                    <div className="text-xs text-gray-400">Người nhận</div>
                  </div>
                </div>
                <div className="flex items-start gap-2.5">
                  <Phone className="h-4.5 w-4.5 text-gray-400 mt-0.5 flex-shrink-0" />
                  <div>
                    <div className="font-bold text-gray-800">{order.receiverPhoneSnapshot}</div>
                    <div className="text-xs text-gray-400">Số điện thoại</div>
                  </div>
                </div>
                <div className="flex items-start gap-2.5">
                  <MapPin className="h-4.5 w-4.5 text-gray-400 mt-0.5 flex-shrink-0" />
                  <div>
                    <div className="font-medium text-gray-800 leading-relaxed">
                      {order.shippingAddressSnapshot}
                    </div>
                    <div className="text-xs text-gray-400">Địa chỉ giao hàng</div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="space-y-3 text-sm text-gray-600">
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-gray-400" />
                  <span>Hình thức: Ăn tại bàn</span>
                </div>
                {order.notes && (
                  <div className="flex items-start gap-2">
                    <FileText className="h-4 w-4 text-gray-400 mt-0.5" />
                    <div>
                      <div className="text-gray-400 text-xs">Ghi chú của khách hàng</div>
                      <p className="font-medium text-gray-700">{order.notes}</p>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Payment info */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 space-y-4">
            <h3 className="text-sm font-bold text-gray-900 uppercase tracking-wider border-b border-gray-100 pb-3">
              Thông tin thanh toán
            </h3>

            <div className="space-y-3.5 text-sm text-gray-600">
              <div className="flex justify-between items-center">
                <span>Phương thức:</span>
                <span className="font-bold text-gray-800">
                  {order.paymentMethod === 'BANK_TRANSFER'
                    ? 'Chuyển khoản ngân hàng'
                    : order.paymentMethod === 'ONLINE_GATEWAY'
                    ? 'Ví điện tử / Thẻ (Online)'
                    : order.paymentMethod === 'COD'
                    ? 'Thanh toán khi nhận hàng (COD)'
                    : 'Tiền mặt'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span>Trạng thái thanh toán:</span>
                <StatusBadge status={order.paymentStatus} />
              </div>
              <div className="flex justify-between items-center border-t border-gray-100 pt-3">
                <span className="font-bold text-gray-800">Tổng cộng:</span>
                <span className="font-bold text-lg text-[#0F6B4F]">
                  {formatPrice(order.finalAmount)}
                </span>
              </div>
            </div>
          </div>

          {/* Customer Reviews Section */}
          {order.status === 'HOAN_THANH' && (
            <div id="review-section" className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 space-y-4">
              <h3 className="text-sm font-bold text-gray-900 uppercase tracking-wider border-b border-gray-100 pb-3">
                Đánh giá đơn hàng
              </h3>

              {existingReview ? (
                // Read-only existing review
                <div className="space-y-3.5">
                  <div className="flex items-center justify-between">
                    <div className="flex gap-1">
                      {[1, 2, 3, 4, 5].map((s) => (
                        <Star
                          key={s}
                          className={`h-5 w-5 ${
                            s <= existingReview.rating
                              ? 'fill-[#C7A45B] text-[#C7A45B]'
                              : 'text-gray-200'
                          }`}
                        />
                      ))}
                    </div>
                    <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-amber-50 text-amber-700 border border-amber-100">
                      {existingReview.moderationStatus === 'APPROVED'
                        ? 'Đã duyệt'
                        : existingReview.moderationStatus === 'REJECTED'
                        ? 'Từ chối duyệt'
                        : 'Đang kiểm duyệt'}
                    </span>
                  </div>
                  {existingReview.comment ? (
                    <p className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3 italic">
                      &ldquo;{existingReview.comment}&rdquo;
                    </p>
                  ) : (
                    <p className="text-sm text-gray-400 italic">Không có bình luận.</p>
                  )}
                  <div className="text-[10px] text-gray-400 text-right">
                    Đã gửi lúc {formatDate(existingReview.createdAt)}
                  </div>
                </div>
              ) : order.canReview ? (
                // Write Review Form
                <form onSubmit={handleSubmit(onSubmitReview)} className="space-y-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-gray-600 uppercase">
                      Đánh giá chất lượng (1-5 sao):
                    </label>
                    <div className="flex gap-1.5">
                      {[1, 2, 3, 4, 5].map((s) => (
                        <button
                          key={s}
                          type="button"
                          onClick={() => setValue('rating', s)}
                          onMouseEnter={() => setHoverRating(s)}
                          onMouseLeave={() => setHoverRating(null)}
                          className="focus:outline-none transition duration-150 transform hover:scale-110"
                        >
                          <Star
                            className={`h-8 w-8 ${
                              s <= (hoverRating ?? rating)
                                ? 'fill-[#C7A45B] text-[#C7A45B]'
                                : 'text-gray-200'
                            }`}
                          />
                        </button>
                      ))}
                    </div>
                    {errors.rating && (
                      <p className="text-xs text-red-500">{errors.rating.message}</p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <div className="flex justify-between items-center">
                      <label className="text-xs font-bold text-gray-600 uppercase">
                        Bình luận ý kiến (Tối đa 1000 ký tự):
                      </label>
                      <span className="text-[10px] text-gray-400 font-mono">
                        {commentValue.length}/1000
                      </span>
                    </div>
                    <Textarea
                      {...register('comment')}
                      placeholder="Chia sẻ trải nghiệm của bạn về món ăn và dịch vụ..."
                      rows={4}
                      className="text-sm"
                      maxLength={1000}
                    />
                    {errors.comment && (
                      <p className="text-xs text-red-500">{errors.comment.message}</p>
                    )}
                  </div>

                  <Button
                    type="submit"
                    className="w-full bg-[#0F6B4F] hover:bg-[#0c5942] text-white text-sm font-bold"
                    disabled={createReviewMutation.isPending}
                  >
                    {createReviewMutation.isPending ? 'Đang gửi...' : 'Gửi đánh giá'}
                  </Button>
                </form>
              ) : (
                <div className="text-sm text-gray-400 italic text-center py-2">
                  Bạn không thể đánh giá đơn hàng này.
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
