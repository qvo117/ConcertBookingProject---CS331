package asg.concert.service.mapper;

import asg.concert.common.dto.UserDTO;
import asg.concert.service.domain.User;

public class UserMapper {
	static User toDomainModel(UserDTO dto) {
		User user = new User(
				dto.getUsername(),
				dto.getPassword());
		return user;
	}
	
	static UserDTO toDto(User user) {
		UserDTO dto = new UserDTO(
				user.getUsername(),
				user.getPassword());
		return dto;
	}
}