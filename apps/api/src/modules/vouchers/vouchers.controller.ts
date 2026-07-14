import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { VouchersService } from './vouchers.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-08, UC-24
@ApiTags('Vouchers')
@Controller('vouchers')
export class VouchersController {
  constructor(private readonly vouchersService: VouchersService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách vouchers' })
  findAll() {
    return this.vouchersService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết vouchers theo ID' })
  findOne(@Param('id') id: string) {
    return this.vouchersService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới vouchers' })
  create(@Body() dto: any) {
    return this.vouchersService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật vouchers' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.vouchersService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa vouchers' })
  remove(@Param('id') id: string) {
    return this.vouchersService.remove(id);
  }
}
