package asg.concert.service.domain;

import javax.persistence.*;
import javax.ws.rs.container.AsyncResponse;

import asg.concert.common.dto.ConcertInfoSubscriptionDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Set;

@Entity
@Table(name = "USERS")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String username;
	private String password;

	@Version
	private Long version;

    private ArrayList<ConcertInfoSubscriptionDTO> subs = new ArrayList<ConcertInfoSubscriptionDTO>();
	
	public User() {}
	
	public User(Long id, String username, String password) {
		this.id = id;
		this.username = username;
		this.password = password;
	}
	
	public User(String username, String password) {
		this(null, username, password);
	}
	
	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
    	return username;
    }
    
    public void setUsername(String username) {
    	this.username = username;
    }
    
    public String getPassword() {
    	return password;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }

    public ArrayList<ConcertInfoSubscriptionDTO> getSubscriptions() {
        return subs;
    }

    public void addSubscriptions(ConcertInfoSubscriptionDTO sub) {
        this.subs.add(sub);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("User, id: ");
        buffer.append(id);
        buffer.append(", username: ");
        buffer.append(username);
        buffer.append(", password: ");
        buffer.append(password);
        buffer.append(", version: ");
        buffer.append(version.toString());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder()
        		.append(username, rhs.username)
                .append(password, rhs.password)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
        		.append(username)
                .append(password)
                .toHashCode();
    }
}