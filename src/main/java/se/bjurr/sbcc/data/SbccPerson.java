package se.bjurr.sbcc.data;

public class SbccPerson {

 private final String emailAddress;

 private final String name;

 public SbccPerson(String name, String emailAddress) {
  this.name = name;
  this.emailAddress = emailAddress;
 }

 public String getEmailAddress() {
  return emailAddress;
 }

 public String getName() {
  return name;
 }
}
