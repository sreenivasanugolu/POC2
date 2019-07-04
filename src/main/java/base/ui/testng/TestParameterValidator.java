package base.ui.testng;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class TestParameterValidator implements IInvokedMethodListener {
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult){
        Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        Parameters paramAnnotation = ((Method) testMethod).getAnnotation(Parameters.class);
        if(paramAnnotation != null){
            String[] params = (paramAnnotation).value();
            for (String param : params ) {
                String value = testResult.getTestContext().getCurrentXmlTest().getParameter(param);
                if(value == null){
                    String msg = "Parameter " + param + " for method " + testMethod.getName() +
                            " in class " +testMethod.getDeclaringClass().getName() +
                            " was not found in test site file!" ;
                    throw new RuntimeException(msg);
                }
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult){

    }
}
