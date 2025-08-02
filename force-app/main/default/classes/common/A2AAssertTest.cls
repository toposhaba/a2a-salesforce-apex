/**
 * Test class for A2AAssert utility
 */
@isTest
private class A2AAssertTest {
    
    @isTest
    static void testCheckNotNullParam_WithValidValue_ReturnsValue() {
        // Given
        String paramName = 'testParam';
        String paramValue = 'testValue';
        
        // When
        Object result = A2AAssert.checkNotNullParam(paramName, paramValue);
        
        // Then
        System.assertEquals(paramValue, result, 'Should return the same value');
    }
    
    @isTest
    static void testCheckNotNullParam_WithNullValue_ThrowsException() {
        // Given
        String paramName = 'testParam';
        String paramValue = null;
        
        // When/Then
        try {
            A2AAssert.checkNotNullParam(paramName, paramValue);
            System.assert(false, 'Should have thrown exception');
        } catch (A2AException e) {
            System.assert(e.getMessage().contains(paramName), 'Exception message should contain parameter name');
        }
    }
    
    @isTest
    static void testIsNullOrStringOrInteger_WithNull_NoException() {
        // When/Then - should not throw
        A2AAssert.isNullOrStringOrInteger(null);
    }
    
    @isTest
    static void testIsNullOrStringOrInteger_WithString_NoException() {
        // When/Then - should not throw
        A2AAssert.isNullOrStringOrInteger('test');
    }
    
    @isTest
    static void testIsNullOrStringOrInteger_WithInteger_NoException() {
        // When/Then - should not throw
        A2AAssert.isNullOrStringOrInteger(123);
    }
    
    @isTest
    static void testIsNullOrStringOrInteger_WithInvalidType_ThrowsException() {
        // Given
        Decimal invalidValue = 123.45;
        
        // When/Then
        try {
            A2AAssert.isNullOrStringOrInteger(invalidValue);
            System.assert(false, 'Should have thrown exception');
        } catch (A2AException e) {
            System.assert(e.getMessage().contains('must be null, a String, or an Integer'), 
                         'Exception message should indicate valid types');
        }
    }
}