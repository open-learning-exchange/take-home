/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.5
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.chilkatsoft;

public class CkDtObj {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected CkDtObj(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CkDtObj obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        chilkatJNI.delete_CkDtObj(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public CkDtObj() {
    this(chilkatJNI.new_CkDtObj(), true);
  }

  public boolean get_Utc() {
    return chilkatJNI.CkDtObj_get_Utc(swigCPtr, this);
  }

  public void put_Utc(boolean newVal) {
    chilkatJNI.CkDtObj_put_Utc(swigCPtr, this, newVal);
  }

  public int get_Year() {
    return chilkatJNI.CkDtObj_get_Year(swigCPtr, this);
  }

  public void put_Year(int newVal) {
    chilkatJNI.CkDtObj_put_Year(swigCPtr, this, newVal);
  }

  public int get_Month() {
    return chilkatJNI.CkDtObj_get_Month(swigCPtr, this);
  }

  public void put_Month(int newVal) {
    chilkatJNI.CkDtObj_put_Month(swigCPtr, this, newVal);
  }

  public int get_Day() {
    return chilkatJNI.CkDtObj_get_Day(swigCPtr, this);
  }

  public void put_Day(int newVal) {
    chilkatJNI.CkDtObj_put_Day(swigCPtr, this, newVal);
  }

  public int get_Hour() {
    return chilkatJNI.CkDtObj_get_Hour(swigCPtr, this);
  }

  public void put_Hour(int newVal) {
    chilkatJNI.CkDtObj_put_Hour(swigCPtr, this, newVal);
  }

  public int get_Minute() {
    return chilkatJNI.CkDtObj_get_Minute(swigCPtr, this);
  }

  public void put_Minute(int newVal) {
    chilkatJNI.CkDtObj_put_Minute(swigCPtr, this, newVal);
  }

  public int get_Second() {
    return chilkatJNI.CkDtObj_get_Second(swigCPtr, this);
  }

  public void put_Second(int newVal) {
    chilkatJNI.CkDtObj_put_Second(swigCPtr, this, newVal);
  }

  public int get_StructTmYear() {
    return chilkatJNI.CkDtObj_get_StructTmYear(swigCPtr, this);
  }

  public void put_StructTmYear(int newVal) {
    chilkatJNI.CkDtObj_put_StructTmYear(swigCPtr, this, newVal);
  }

  public int get_StructTmMonth() {
    return chilkatJNI.CkDtObj_get_StructTmMonth(swigCPtr, this);
  }

  public void put_StructTmMonth(int newVal) {
    chilkatJNI.CkDtObj_put_StructTmMonth(swigCPtr, this, newVal);
  }

  public boolean Serialize(CkString outStr) {
    return chilkatJNI.CkDtObj_Serialize(swigCPtr, this, CkString.getCPtr(outStr), outStr);
  }

  public String serialize() {
    return chilkatJNI.CkDtObj_serialize(swigCPtr, this);
  }

  public void DeSerialize(String serializedDtObj) {
    chilkatJNI.CkDtObj_DeSerialize(swigCPtr, this, serializedDtObj);
  }

}
