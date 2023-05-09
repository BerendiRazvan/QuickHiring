package domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "imageData")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageData implements Serializable {

    @Id
    @Column(name = "ID", unique = true)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "WIDTH")
    private int width;

    @Column(name = "HEIGHT")
    private int height;

    @Lob
    @Column(name = "IMAGE_DATA")
    private byte[] imageData;

    @Override
    public String toString() {
        return "ImageData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}