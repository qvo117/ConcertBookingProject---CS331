package asg.concert.service.mapper;

import asg.concert.common.dto.UserDTO;
import asg.concert.service.domain.User;

public class UserMapper {
	public static User toDomainModel(UserDTO dtoUser) {
		User user = new User(
				dtoUser.getUsername(),
				dtoUser.getPassword());
		return user;
	}
	
	public static UserDTO toDto(User user) {
		UserDTO dtoUser = new UserDTO(
				user.getUsername(),
				user.getPassword());
		return dtoUser;
	}
}