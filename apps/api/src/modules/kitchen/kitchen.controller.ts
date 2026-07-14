import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { KitchenService } from './kitchen.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-20
@ApiTags('Kitchen')
@Controller('kitchen')
export class KitchenController {
  constructor(private readonly kitchenService: KitchenService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách kitchen' })
  findAll() {
    return this.kitchenService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết kitchen theo ID' })
  findOne(@Param('id') id: string) {
    return this.kitchenService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới kitchen' })
  create(@Body() dto: any) {
    return this.kitchenService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật kitchen' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.kitchenService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa kitchen' })
  remove(@Param('id') id: string) {
    return this.kitchenService.remove(id);
  }
}
