package app.entities;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class WorkoutProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private int trainingDaysPerWeek;

    public WorkoutProgram(String name, String description, int trainingDaysPerWeek) {
        this.name = name;
        this.description = description;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
    }
}
