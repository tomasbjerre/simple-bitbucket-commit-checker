package se.bjurr.sbcc;

public interface SbccUserAdminService {
  boolean emailExists(String emailAddress);

  boolean displayNameExists(String name);

  boolean slugExists(String name);
}
