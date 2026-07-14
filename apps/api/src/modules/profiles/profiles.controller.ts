import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { ProfilesService } from './profiles.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';

// Relevant Use Cases: UC-03
@ApiTags('Profiles')
@Controller('profiles')
export class ProfilesController {
  constructor(private readonly profilesService: ProfilesService) {}

  @Get()
  @ApiOperation({ summary: 'Lấy danh sách profiles' })
  findAll() {
    return this.profilesService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Lấy chi tiết profiles theo ID' })
  findOne(@Param('id') id: string) {
    return this.profilesService.findOne(id);
  }

  @Post()
  @ApiOperation({ summary: 'Tạo mới profiles' })
  create(@Body() dto: any) {
    return this.profilesService.create(dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Cập nhật profiles' })
  update(@Param('id') id: string, @Body() dto: any) {
    return this.profilesService.update(id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Xóa profiles' })
  remove(@Param('id') id: string) {
    return this.profilesService.remove(id);
  }
}
