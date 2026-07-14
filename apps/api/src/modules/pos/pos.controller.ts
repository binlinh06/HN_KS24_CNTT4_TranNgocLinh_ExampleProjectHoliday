import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { PosService } from './pos.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-17, UC-18, UC-19
@ApiTags('Pos')
@Controller('pos')
export class PosController {
  constructor(private readonly posService: PosService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách pos' })
  findAll() {
    return this.posService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết pos theo ID' })
  findOne(@Param('id') id: string) {
    return this.posService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới pos' })
  create(@Body() dto: any) {
    return this.posService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật pos' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.posService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa pos' })
  remove(@Param('id') id: string) {
    return this.posService.remove(id);
  }
}
