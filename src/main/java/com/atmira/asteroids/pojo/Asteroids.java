package com.atmira.asteroids.pojo;

import java.util.Date;

public class Asteroids implements Cloneable {

	private String name;
	private Double diameter;
	private Double velocity;
	private Date date;
	private String planet;

	// Constructor por defecto
	public Asteroids() {

	}

	// Constructor con parametros
	public Asteroids(String _name, Double _diameter, Double _velocity, Date _date, String _planet, Boolean _marked) {
		this.name = _name;
		this.diameter = _diameter;
		this.velocity = _velocity;
		this.date = _date;
		this.planet = _planet;
	}

	// Constructor de copia
	public Asteroids(Asteroids asteroid) {
		this.name = asteroid.name;
		this.diameter = asteroid.diameter;
		this.velocity = asteroid.velocity;
		this.date = asteroid.date;
		this.planet = asteroid.planet;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getDiameter() {
		return diameter;
	}

	public void setDiameter(Double diameter) {
		this.diameter = diameter;
	}

	public Double getVelocity() {
		return velocity;
	}

	public void setVelocity(Double velocity) {
		this.velocity = velocity;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getPlanet() {
		return planet;
	}

	public void setPlanet(String planet) {
		this.planet = planet;
	}

	@Override
	public String toString() {
		return "Asteroids [name=" + name + ", diameter=" + diameter + ", velocity=" + velocity + ", date=" + date
				+ ", planet=" + planet + "]";
	}

}
