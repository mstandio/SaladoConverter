package zoomifytiler;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import org.junit.Test;
import static org.junit.Assert.*;

public class ZoomifyTilerTest {

    private ZoomifyTiler instance;

    public ZoomifyTilerTest() {
        instance = new ZoomifyTiler();
    }

    private Object invokePrivateMethod(Object test, String methodName, Object params[]) throws Exception {
        Object ret = null;
        final Method[] methods =
                test.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals(methodName)) {
                methods[i].setAccessible(true);
                ret = methods[i].invoke(test, params);
                break;
            }
        }
        return ret;
    }

    @Test
    public void numTiers() throws Exception {
        // default tile size of 512
        int result;
        result = ((Integer) invokePrivateMethod(instance, "numTiers", new Object[]{512, 512})).intValue();
        assertEquals(0, result);
        result = ((Integer) invokePrivateMethod(instance, "numTiers", new Object[]{520, 520})).intValue();
        assertEquals(1, result);
        result = ((Integer) invokePrivateMethod(instance, "numTiers", new Object[]{1024, 1024})).intValue();
        assertEquals(1, result);
        result = ((Integer) invokePrivateMethod(instance, "numTiers", new Object[]{1030, 1030})).intValue();
        assertEquals(2, result);
    }

    @Test
    public void sizeAtTier() throws Exception {
        // default tile size of 512    
        Point2D result = null;
        result = (Point2D) invokePrivateMethod(instance, "sizeAtTier", new Object[]{512, 512, 0});
        assertEquals(512d, result.getX(), 0.1d);
        assertEquals(512d, result.getY(), 0.1d);

        result = (Point2D) invokePrivateMethod(instance, "sizeAtTier", new Object[]{520, 520, 0});
        assertEquals(260d, result.getX(), 0.1d);
        assertEquals(260d, result.getY(), 0.1d);

        result = (Point2D) invokePrivateMethod(instance, "sizeAtTier", new Object[]{1024, 1024, 0});
        assertEquals(512d, result.getX(), 0.1d);
        assertEquals(512d, result.getY(), 0.1d);

        result = (Point2D) invokePrivateMethod(instance, "sizeAtTier", new Object[]{1030, 1030, 0});
        assertEquals(258d, result.getX(), 0.1d);
        assertEquals(258d, result.getY(), 0.1d);
    }
    
    @Test
    public void numTilesFromTier() throws Exception {
        // default tile size of 512    
        int result;
        result = ((Integer) invokePrivateMethod(instance, "numTilesFromTier", new Object[]{512, 512, 0})).intValue();        
        assertEquals(1, result);
        
        result = ((Integer) invokePrivateMethod(instance, "numTilesFromTier", new Object[]{520, 520, 1})).intValue();        
        assertEquals(4, result);
        
        result = ((Integer) invokePrivateMethod(instance, "numTilesFromTier", new Object[]{1030, 1030, 2})).intValue();
        assertEquals(9, result);
    }
    
    @Test
    public void numTilesTotal() throws Exception {
        // default tile size of 512
        int result;
        result = ((Integer) invokePrivateMethod(instance, "numTilesTotal", new Object[]{512, 512})).intValue();
        assertEquals(1, result);
        
        result = ((Integer) invokePrivateMethod(instance, "numTilesTotal", new Object[]{520, 520})).intValue();        
        assertEquals(5, result);
        
        result = ((Integer) invokePrivateMethod(instance, "numTilesTotal", new Object[]{1030, 1030})).intValue();
        assertEquals(14, result);        
    }
}
