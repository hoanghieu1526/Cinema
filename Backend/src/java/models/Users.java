/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import enums.UserGender;
import java.time.LocalDateTime;

/**
 *
 * @author NGUYEN HOANG HIEU
 */
public class Users {
    private int userID;
    private String fullName;
    private String userName;
    private String passwordHash;
    private String phone;
    private String email;
    private String imageURL;
    private UserGender gender;
    private LocalDateTime dateOfBirth;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    public Users() {
    }

    public Users(int userID, String fullName, String userName, String passwordHash, String phone, String email, String imageURL, UserGender gender, LocalDateTime dateOfBirth, LocalDateTime createdAt, LocalDateTime updateAt) {
        this.userID = userID;
        this.fullName = fullName;
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.email = email;
        this.imageURL = imageURL;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
    }

    public Users(String fullName, String userName, String passwordHash, String phone, String email, String imageURL, UserGender gender, LocalDateTime dateOfBirth) {
        this.fullName = fullName;
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.email = email;
        this.imageURL = (imageURL != null && !imageURL.trim().isEmpty()) ? imageURL : "../images/default_image/haiz.png";
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = LocalDateTime.now();
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    @Override
    public String toString() {
        return "Users{" + "userID=" + userID + ", fullName=" + fullName + ", userName=" + userName + ", passwordHash=" + passwordHash + ", phone=" + phone + ", email=" + email + ", imageURL=" + imageURL + ", gender=" + gender + ", dateOfBirth=" + dateOfBirth + ", createdAt=" + createdAt + ", updateAt=" + updateAt + '}';
    }
}
