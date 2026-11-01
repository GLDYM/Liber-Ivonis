package com.liberivonis.integration;

final class ScreenArgumentResolver {
    private ScreenArgumentResolver() {
    }

    static Object[] resolve(Class<?>[] parameterTypes, Object[] commonArgs) {
        Object[] result = new Object[parameterTypes.length];
        boolean[] used = new boolean[commonArgs.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == boolean.class || parameterTypes[i] == Boolean.class) {
                result[i] = false;
                continue;
            }
            boolean found = false;
            for (int j = 0; j < commonArgs.length; j++) {
                Object value = commonArgs[j];
                if (used[j] || (value != null && !parameterTypes[i].isInstance(value)))
                    continue;
                if (value == null && parameterTypes[i].isPrimitive())
                    continue;
                result[i] = value;
                used[j] = true;
                found = true;
                break;
            }
            if (!found)
                return null;
        }
        return result;
    }
}
