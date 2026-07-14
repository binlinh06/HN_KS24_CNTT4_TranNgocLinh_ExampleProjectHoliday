import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-01, UC-02
@ApiTags('Auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) { }

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách auth' })
  findAll() {
    return this.authService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết auth theo ID' })
  findOne(@Param('id') id: string) {
    return this.authService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới auth' })
  create(@Body() dto: any) {
    return this.authService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật auth' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.authService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa auth' })
  remove(@Param('id') id: string) {
    return this.authService.remove(id);
  }
}
