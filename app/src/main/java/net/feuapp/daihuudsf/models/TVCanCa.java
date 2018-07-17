package net.feuapp.daihuudsf.models;

/**
 * Created by linh3 on 09/04/2018.
 */

public class TVCanCa {
    private int id;
    private int serverkey;
    private long rkey;
    private long rkeythuyenvien;
    private String ngayps;
    private String notes;
    private String tenhs;
    private String soluong;
    private String dongia;
    private String giamgia;
    private String thanhtien;
    private String updatetime;
    private String username;

    public TVCanCa() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerkey() {
        return serverkey;
    }

    public void setServerkey(int serverkey) {
        this.serverkey = serverkey;
    }

    public long getRkey() {
        return rkey;
    }

    public void setRkey(long rkey) {
        this.rkey = rkey;
    }

    public long getRkeythuyenvien() {
        return rkeythuyenvien;
    }

    public void setRkeythuyenvien(long rkeythuyenvien) {
        this.rkeythuyenvien = rkeythuyenvien;
    }

    public String getNgayps() {
        return ngayps;
    }

    public void setNgayps(String ngayps) {
        this.ngayps = ngayps;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTenhs() {
        return tenhs;
    }

    public void setTenhs(String tenhs) {
        this.tenhs = tenhs;
    }

    public String getSoluong() {
        return soluong;
    }

    public void setSoluong(String soluong) {
        this.soluong = soluong;
    }

    public String getDongia() {
        return dongia;
    }

    public void setDongia(String dongia) {
        this.dongia = dongia;
    }

    public String getGiamgia() {
        return giamgia;
    }

    public void setGiamgia(String giamgia) {
        this.giamgia = giamgia;
    }

    public String getThanhtien() {
        return thanhtien;
    }

    public void setThanhtien(String thanhtien) {
        this.thanhtien = thanhtien;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
