import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { ShiftsService } from './shifts.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-28
@ApiTags('Shifts')
@Controller('shifts')
export class ShiftsController {
  constructor(private readonly shiftsService: ShiftsService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách shifts' })
  findAll() {
    return this.shiftsService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết shifts theo ID' })
  findOne(@Param('id') id: string) {
    return this.shiftsService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới shifts' })
  create(@Body() dto: any) {
    return this.shiftsService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật shifts' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.shiftsService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa shifts' })
  remove(@Param('id') id: string) {
    return this.shiftsService.remove(id);
  }
}
