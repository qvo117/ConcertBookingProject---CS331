package asg.concert.service.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import asg.concert.common.types.*;

/**
 * Class to represent a Performer (an artist or band that plays at Concerts). A
 * Performer object has an ID (a database primary key value), a name, the name
 * of an image file, and a genre.
 */
@Entity
public class Performer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
    private Long id;
    private String name;
    
    @Column(name = "IMAGE_NAME", nullable = false)
    private String imageUri;
    
    @Enumerated(EnumType.STRING)
    private Genre genre;
    private String blurb;

    public Performer() { }

    public Performer(Long id, String name, String imageUri, Genre genre, String blurb) {
        this.id = id;
        this.name = name;
        this.imageUri = imageUri;
        this.genre = genre;
        this.blurb = blurb;
    }

    public Performer(String name, String imageUri, Genre genre, String blurb) {
        this(null, name, imageUri, genre, blurb);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }
    
    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Performer, id: ");
        buffer.append(id);
        buffer.append(", name: ");
        buffer.append(name);
        buffer.append(", image: ");
        buffer.append(imageUri);
        buffer.append(", genre: ");
        buffer.append(genre.toString());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Performer))
            return false;
        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;
        return new EqualsBuilder().
                append(name, rhs.name).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(name).hashCode();
    }
}