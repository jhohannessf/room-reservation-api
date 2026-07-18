package br.com.jhohannesfreitas.roomreservationapi.domain.entity;

import br.com.jhohannesfreitas.roomreservationapi.domain.enums.StatusSala;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salas")
public class Sala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer numero;

    @Column(nullable = false)
    @Positive
    private Integer capacidade;

    @Enumerated(EnumType.STRING)
    private StatusSala status = StatusSala.LIVRE;

    @OneToMany(mappedBy = "sala")
    private List<Reserva> reservas = new ArrayList<>();

    public Sala() {}

    public Sala(int numero, int capacidade) {
        this.numero = numero;
        this.capacidade = capacidade;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumero() {
        return numero;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public StatusSala getStatus() {
        return status;
    }

    public void setStatus(StatusSala status) {
        this.status = status;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }
}
