package cn.alien95.resthttplibrary.data.bean;

/**
 * Created by linlongxin on 2016/3/24.
 */
public class UserInfo {

    private int id;
    private String face;
    private String name;
    private String sign;
    private int gender;
    private String school;
    private int age;
    private long birth;
    private String major;
    private String phone;
    private String qq;
    private String intro;
    private String loverSpace;
    private String lovePassword;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public long getBirth() {
        return birth;
    }

    public void setBirth(long birth) {
        this.birth = birth;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getLoverSpace() {
        return loverSpace;
    }

    public void setLoverSpace(String loverSpace) {
        this.loverSpace = loverSpace;
    }

    public String getLovePassword() {
        return lovePassword;
    }

    public void setLovePassword(String lovePassword) {
        this.lovePassword = lovePassword;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", face='" + face + '\'' +
                ", name='" + name + '\'' +
                ", sign='" + sign + '\'' +
                ", gender=" + gender +
                ", school='" + school + '\'' +
                ", age=" + age +
                ", birth=" + birth +
                ", major='" + major + '\'' +
                ", phone='" + phone + '\'' +
                ", qq='" + qq + '\'' +
                ", intro='" + intro + '\'' +
                ", loverSpace='" + loverSpace + '\'' +
                ", lovePassword='" + lovePassword + '\'' +
                '}';
    }
}
