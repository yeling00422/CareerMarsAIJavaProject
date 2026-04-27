package com.example.careermarsaiproject.vo;

import lombok.Data;

import java.util.List;
@Data
public class PerformanceAnalysis {
    private int culturalCompatibility;
    private int resumeMatchingScore;
    private int overallPerformance;
    private List<Improvement> improvements;
//    private List<MentorResultVo> mentorList;


//    public static class Improvement {
//        private String title;
//        private String description;
//
//        // Getters and Setters
//        public String getTitle() {
//            return title;
//        }
//
//        public void setTitle(String title) {
//            this.title = title;
//        }
//
//        public String getDescription() {
//            return description;
//        }
//
//        public void setDescription(String description) {
//            this.description = description;
//        }
//    }

//    public static class Mentor {
//
//
//        // Getters and Setters
//        public int getMarryRate() {
//            return marryRate;
//        }
//
//        public void setMarryRate(int marryRate) {
//            this.marryRate = marryRate;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getLableNames() {
//            return lableNames;
//        }
//
//        public void setLableNames(String lableNames) {
//            this.lableNames = lableNames;
//        }
//
//        public double getRating() {
//            return rating;
//        }
//
//        public void setRating(double rating) {
//            this.rating = rating;
//        }
//
//        public int getStudentCount() {
//            return studentCount;
//        }
//
//        public void setStudentCount(int studentCount) {
//            this.studentCount = studentCount;
//        }
//
//        public int getPlacementRate() {
//            return placementRate;
//        }
//
//        public void setPlacementRate(int placementRate) {
//            this.placementRate = placementRate;
//        }
//
//        public List<String> getReasons() {
//            return reasons;
//        }
//
//        public void setReasons(List<String> reasons) {
//            this.reasons = reasons;
//        }
//    }
}