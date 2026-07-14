import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { CartsService } from './carts.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-07
@ApiTags('Carts')
@Controller('carts')
export class CartsController {
  constructor(private readonly cartsService: CartsService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách carts' })
  findAll() {
    return this.cartsService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết carts theo ID' })
  findOne(@Param('id') id: string) {
    return this.cartsService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới carts' })
  create(@Body() dto: any) {
    return this.cartsService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật carts' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.cartsService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa carts' })
  remove(@Param('id') id: string) {
    return this.cartsService.remove(id);
  }
}
