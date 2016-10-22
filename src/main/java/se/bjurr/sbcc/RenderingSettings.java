package se.bjurr.sbcc;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Map;

import com.atlassian.bitbucket.setting.Settings;

public class RenderingSettings implements Settings {

 private final SbccRenderer sbccRenderer;
 private final Settings settings;

 public RenderingSettings(Settings settings, SbccRenderer sbccRenderer) {
  this.settings = settings;
  this.sbccRenderer = sbccRenderer;
 }

 @Override
 public Map<String, Object> asMap() {
  throw new RuntimeException("Not implemented!");
 }

 @Override
 public Boolean getBoolean(String arg0) {
  return this.settings.getBoolean(arg0);
 }

 @Override
 public boolean getBoolean(String arg0, boolean arg1) {
  throw new RuntimeException("Not implemented!");
 }

 @Override
 public Double getDouble(String arg0) {
  throw new RuntimeException("Not implemented!");
 }

 @Override
 public double getDouble(String arg0, double arg1) {
  throw new RuntimeException("Not implemented!");
 }

 @Override
 public Integer getInt(String arg0) {
  throw new RuntimeException("Not implemented!");
 }

 @Override
 public int getInt(String arg0, int arg1) {
  throw new RuntimeException("Not implemented!");
 }

 @Override
 public Long getLong(String arg0) {
  throw new RuntimeException("Not implemented!");
 }

 @Override
 public long getLong(String arg0, long arg1) {
  throw new RuntimeException("Not implemented!");
 }

 @Override
 public String getString(String arg0) {
  String string = this.settings.getString(arg0);
  if (isNullOrEmpty(string)) {
   return string;
  }
  return this.sbccRenderer.render(string);
 }

 @Override
 public String getString(String arg0, String arg1) {
  throw new RuntimeException("Not implemented!");
 }

}
