import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { SystemConfigService } from './system-config.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-26
@ApiTags('SystemConfig')
@Controller('system-config')
export class SystemConfigController {
  constructor(private readonly systemConfigService: SystemConfigService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách system-config' })
  findAll() {
    return this.systemConfigService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết system-config theo ID' })
  findOne(@Param('id') id: string) {
    return this.systemConfigService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới system-config' })
  create(@Body() dto: any) {
    return this.systemConfigService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật system-config' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.systemConfigService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa system-config' })
  remove(@Param('id') id: string) {
    return this.systemConfigService.remove(id);
  }
}
