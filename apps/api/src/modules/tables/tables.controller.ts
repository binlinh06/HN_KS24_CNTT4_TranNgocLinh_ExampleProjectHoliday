import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { TablesService } from './tables.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-16
@ApiTags('Tables')
@Controller('tables')
export class TablesController {
  constructor(private readonly tablesService: TablesService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách tables' })
  findAll() {
    return this.tablesService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết tables theo ID' })
  findOne(@Param('id') id: string) {
    return this.tablesService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới tables' })
  create(@Body() dto: any) {
    return this.tablesService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật tables' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.tablesService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa tables' })
  remove(@Param('id') id: string) {
    return this.tablesService.remove(id);
  }
}
