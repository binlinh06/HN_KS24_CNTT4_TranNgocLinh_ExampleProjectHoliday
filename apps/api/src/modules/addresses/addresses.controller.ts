import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { AddressesService } from './addresses.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-04
@ApiTags('Addresses')
@Controller('addresses')
export class AddressesController {
  constructor(private readonly addressesService: AddressesService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách addresses' })
  findAll() {
    return this.addressesService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết addresses theo ID' })
  findOne(@Param('id') id: string) {
    return this.addressesService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới addresses' })
  create(@Body() dto: any) {
    return this.addressesService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật addresses' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.addressesService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa addresses' })
  remove(@Param('id') id: string) {
    return this.addressesService.remove(id);
  }
}
