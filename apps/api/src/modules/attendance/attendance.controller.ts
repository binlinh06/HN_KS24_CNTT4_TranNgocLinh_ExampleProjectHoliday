import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { AttendanceService } from './attendance.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-28
@ApiTags('Attendance')
@Controller('attendance')
export class AttendanceController {
  constructor(private readonly attendanceService: AttendanceService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách attendance' })
  findAll() {
    return this.attendanceService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết attendance theo ID' })
  findOne(@Param('id') id: string) {
    return this.attendanceService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới attendance' })
  create(@Body() dto: any) {
    return this.attendanceService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật attendance' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.attendanceService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa attendance' })
  remove(@Param('id') id: string) {
    return this.attendanceService.remove(id);
  }
}
