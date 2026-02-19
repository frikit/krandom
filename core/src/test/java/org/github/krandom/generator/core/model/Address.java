/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

/**
 * Flat POJO covering every Java base type.
 * Used to verify that {@link org.github.krandom.generator.object.ObjectGenerator}
 * populates all primitive and wrapper field types correctly.
 */
public class Address {

    private String street;
    private int    houseNumber;
    private byte   floor;
    private short  postCode;
    private long   geoId;
    private double latitude;
    private float  longitude;
    private boolean active;
    private char    countryCode;

    public Address() { }

    public String  getStreet()      { return street; }
    public int     getHouseNumber() { return houseNumber; }
    public byte    getFloor()       { return floor; }
    public short   getPostCode()    { return postCode; }
    public long    getGeoId()       { return geoId; }
    public double  getLatitude()    { return latitude; }
    public float   getLongitude()   { return longitude; }
    public boolean isActive()       { return active; }
    public char    getCountryCode() { return countryCode; }
}
