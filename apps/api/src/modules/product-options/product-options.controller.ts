import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { ProductOptionsService } from './product-options.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-06
@ApiTags('ProductOptions')
@Controller('product-options')
export class ProductOptionsController {
  constructor(private readonly productOptionsService: ProductOptionsService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách product-options' })
  findAll() {
    return this.productOptionsService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết product-options theo ID' })
  findOne(@Param('id') id: string) {
    return this.productOptionsService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới product-options' })
  create(@Body() dto: any) {
    return this.productOptionsService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật product-options' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.productOptionsService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa product-options' })
  remove(@Param('id') id: string) {
    return this.productOptionsService.remove(id);
  }
}
