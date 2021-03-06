package asg.concert.service.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import asg.concert.common.jackson.*;

/*
Concert Domain Model
Domain model for the concert entity used to populate the columns.
 */
@Entity
@Table(name = "CONCERTS")
public class Concert implements Comparable<Concert> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) //Assigned primary keys
	@Column(nullable = false)
	private Long id;                                    // id, title, imageName, blurb all must be filled in
	@Column(nullable = false)
	private String title;

	@Column(name = "IMAGE_NAME", nullable = false)
	private String imageName;
	
	@Column(nullable = false, length = 1024)
	private String blurb;
	
	@ElementCollection(fetch = FetchType.EAGER)         //Load all the date in one query instead of loading one at a time
	@CollectionTable(name = "CONCERT_DATES", joinColumns = @JoinColumn(name = "CONCERT_ID"))    //Concert date table made joining concertid and dates
	@Column(name = "DATE")
	private Set<LocalDateTime> dates = new HashSet<LocalDateTime>();
	

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})//performer is only fetched when relationship is used.
  // Is only persisting when concert is persistent
	@JoinTable(name = "CONCERT_PERFORMER", 
		joinColumns = @JoinColumn(name = "CONCERT_ID"),
		inverseJoinColumns =  @JoinColumn(name = "PERFORMER_ID"))
	private Set<Performer> performers;

	public Concert(Long id, String title, String imageName, String blurb) {     //init concert
        this.id = id;
        this.title = title;
        this.imageName = imageName;
        this.blurb = blurb;
    }

    public Concert(String title, String imageName, String blurb) {
        this(null, title, imageName, blurb);
    }

    public Concert() {
    }
    //all the concert methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getImageName() {
    	return this.imageName;
    }
    
    public void setImageName(String imageName) {
    	this.imageName = imageName;
    }
    
    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    @JsonSerialize(contentUsing = LocalDateTimeSerializer.class)
    @JsonDeserialize(contentUsing = LocalDateTimeDeserializer.class)
    public Set<LocalDateTime> getDates() {
        return dates;
    }
    
    public void addDate(LocalDateTime date) {
        this.dates.add(date);
    }

    public Set<Performer> getPerformers() {
        return performers;
    }
    
    public void addPerformer(Performer performer) {
        this.performers.add(performer);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Concert, id: ");
        buffer.append(id);
        buffer.append(", title: ");
        buffer.append(title);
        buffer.append(", dates: ");
        buffer.append(dates.toString());
        buffer.append(", featuring: ");
        buffer.append(performers.toString());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
                append(title, rhs.title).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(title).hashCode();
    }

    @Override
    public int compareTo(Concert concert) {
        return title.compareTo(concert.getTitle());
    }
}
