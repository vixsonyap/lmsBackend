/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author Vixson
 */
@Entity
public class Certification implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long certificationId;
    @Column
    private String title;
    @Column
    private String description;
    /*@Column
    private String criteria;
    @ManyToMany
    private List<User> userList;*/
    @Column
    private Date dateAchieved;
    @OneToMany
    private List<Coursepack> coursepackList;

    public Certification() {
    }

    public Certification(Long certificationId, String title, String description, Date dateAchieved, List<Coursepack> coursepackList) {
        this.certificationId = certificationId;
        this.title = title;
        this.description = description;
        this.dateAchieved = dateAchieved;
        this.coursepackList = coursepackList;
    }
    
    public Long getId() {
        return certificationId;
    }

    public void setId(Long certificationId) {
        this.certificationId = certificationId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (certificationId != null ? certificationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the certificationId fields are not set
        if (!(object instanceof Certification)) {
            return false;
        }
        Certification other = (Certification) object;
        if ((this.certificationId == null && other.certificationId != null) || (this.certificationId != null && !this.certificationId.equals(other.certificationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Certification[ certificationId=" + certificationId + " ]";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public Date getDateAchieved() {
        return dateAchieved;
    }

    public void setDateAchieved(Date dateAchieved) {
        this.dateAchieved = dateAchieved;
    }

   /* public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }*/

    public List<Coursepack> getCoursepackList() {
        return coursepackList;
    }

    public void setCoursepackList(List<Coursepack> coursepackList) {
        this.coursepackList = coursepackList;
    }
    
}
