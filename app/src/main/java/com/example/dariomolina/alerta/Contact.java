package com.example.dariomolina.alerta;

public class Contact {
    private String name;
    private String phoneNumber;
    private String id;

    public Contact(String name, String phoneNumber, String id) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if(!(o instanceof Contact))
            return false;

        Contact c = (Contact) o;

        return c.phoneNumber == this.phoneNumber;
    }
}
