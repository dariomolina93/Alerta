package dm.android.content.alerta;

public class Contact {
    private String name;
    private String phoneNumber;
    private String id;
    private boolean isSelected;

    public Contact(String name, String phoneNumber, String id) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.isSelected = false;
    }

    public void setIsSelected(boolean isSelected) {this.isSelected = isSelected;}

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

    public boolean getIsSelected() {
        return isSelected;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if(!(o instanceof Contact))
            return false;

        Contact c = (Contact) o;

        return c.phoneNumber.equals(this.phoneNumber);
    }
}
