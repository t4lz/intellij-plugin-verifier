package com.jetbrains.pluginverifier.problems;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Sergey Patrikeev
 */
@XmlRootElement
public class InvokeInterfaceOnPrivateMethodProblem extends Problem {

  private String myMethod;

  public InvokeInterfaceOnPrivateMethodProblem() {
  }

  public InvokeInterfaceOnPrivateMethodProblem(String method) {
    myMethod = method;
  }

  @Override
  public String getDescriptionPrefix() {
    return "attempt to perform 'invokeinterface' on private method";
  }

  public String getMethod() {
    return myMethod;
  }

  public void setMethod(String method) {
    myMethod = method;
  }

  @Override
  public String getDescription() {
    return getDescriptionPrefix() + " " + myMethod;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InvokeInterfaceOnPrivateMethodProblem that = (InvokeInterfaceOnPrivateMethodProblem) o;

    return myMethod != null ? myMethod.equals(that.myMethod) : that.myMethod == null;

  }

  @Override
  public int hashCode() {
    int result = 321;
    result = 31 * result + (myMethod != null ? myMethod.hashCode() : 0);
    return result;
  }
}
