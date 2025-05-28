package Address.models;

import javax.swing.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Contact model class representing a contact with all necessary information
 * Implements Serializable for JSON persistence
 */
public class Contact implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String firstName;
    private String lastName;
    private String phone;
    private String birthDate;
    private String address;
    private String email;
    private transient ImageIcon photo; // transient to avoid serialization issues



    /**
     * Full constructor for creating a new contact
     */
    public Contact(String name, String phone, String firstName, String lastName,
                   String birthDate, String address, String email, ImageIcon photo) {
        this.name = name != null ? name : "";
        this.phone = phone != null ? phone : "";
        this.firstName = firstName != null ? firstName : "";
        this.lastName = lastName != null ? lastName : "";
        this.birthDate = birthDate != null ? birthDate : "";
        this.address = address != null ? address : "";
        this.email = email != null ? email : "";
        this.photo = photo;

        // Auto-generate name if not provided
        if (this.name.isEmpty() && (!this.firstName.isEmpty() || !this.lastName.isEmpty())) {
            this.name = (this.firstName + " " + this.lastName).trim();
        }
    }

    // Getters with null-safe returns
    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public String getFirstName() {
        return firstName != null ? firstName : "";
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName : "";
        updateFullName();
    }

    public String getLastName() {
        return lastName != null ? lastName : "";
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName : "";
        updateFullName();
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone : "";
    }

    public String getBirthDate() {
        return birthDate != null ? birthDate : "";
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate != null ? birthDate : "";
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public void setAddress(String address) {
        this.address = address != null ? address : "";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public ImageIcon getPhoto() {
        return photo;
    }

    public void setPhoto(ImageIcon photo) {
        this.photo = photo;
    }

    /**
     * Updates the full name when first or last name changes
     */
    private void updateFullName() {
        if (firstName != null && lastName != null) {
            this.name = (firstName + " " + lastName).trim();
        }
    }

    /**
     * Validates if the contact has minimum required information
     */
    public boolean isValid() {
        return !getFirstName().isEmpty() && !getLastName().isEmpty();
    }

    /**
     * Gets display name for UI purposes
     */
    public String getDisplayName() {
        String displayName = getName();
        if (displayName.isEmpty()) {
            displayName = (getFirstName() + " " + getLastName()).trim();
        }
        return displayName.isEmpty() ? "Unknown Contact" : displayName;
    }

    /**
     * Formats phone number for display
     */
    public String getFormattedPhone() {
        String phoneNum = getPhone();
        if (phoneNum.length() == 10) {
            return String.format("(%s) %s-%s",
                    phoneNum.substring(0, 3),
                    phoneNum.substring(3, 6),
                    phoneNum.substring(6));
        }
        return phoneNum;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Contact contact = (Contact) obj;
        return Objects.equals(name, contact.name) &&
                Objects.equals(phone, contact.phone) &&
                Objects.equals(email, contact.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone, email);
    }
}