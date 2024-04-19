package com.example.jobnotes;

public class Note {

    private int number; // 0...127
    private int velocity; // 1...127
    private int startTime; // ms
    private int endTime; // ms

    public Note() { }

    public Note(int number, int velocity, int startTime, int endTime) {
        this.number = number;
        this.velocity = velocity;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Implement getters and setters

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
}
