import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { ReportsService } from './reports.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-27
@ApiTags('Reports')
@Controller('reports')
export class ReportsController {
  constructor(private readonly reportsService: ReportsService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách reports' })
  findAll() {
    return this.reportsService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết reports theo ID' })
  findOne(@Param('id') id: string) {
    return this.reportsService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới reports' })
  create(@Body() dto: any) {
    return this.reportsService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật reports' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.reportsService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa reports' })
  remove(@Param('id') id: string) {
    return this.reportsService.remove(id);
  }
}
