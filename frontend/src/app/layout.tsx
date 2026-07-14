import type { Metadata } from "next";
import { Be_Vietnam_Pro } from "next/font/google";
import { Providers } from "@/components/providers";
import "./globals.css";

const beVietnamPro = Be_Vietnam_Pro({
  subsets: ["vietnamese", "latin"],
  weight: ["300", "400", "500", "600", "700"],
  variable: "--font-be-vietnam-pro",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Phở Bò Gia Truyền — Hệ thống Quản lý & Bán hàng",
  description:
    "Hệ thống quản lý chuỗi nhà hàng Phở Bò Gia Truyền. Đặt món trực tuyến, quản lý đơn hàng, bếp, nhân sự và báo cáo.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" className={beVietnamPro.variable}>
      <body className="font-body">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
