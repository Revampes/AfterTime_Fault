package com.aftertime.ratallofyou.UI.newui.config;

import com.aftertime.ratallofyou.UI.newui.annotations.*;
import com.aftertime.ratallofyou.UI.newui.categories.CategoryPanel;
import com.aftertime.ratallofyou.UI.newui.categories.ModulePanel;
import com.aftertime.ratallofyou.config.ModConfig;

import java.lang.reflect.Field;
import java.util.*;

public class UIConfigManager {

    public static Map<String, CategoryPanel> createUICategories() {
        Map<String, CategoryPanel> categories = new HashMap<>();

        // Get all fields from ModConfig class
        Field[] fields = ModConfig.class.getDeclaredFields();

        // Group fields by module key
        Map<String, List<Field>> moduleFields = new HashMap<>();

        for (Field field : fields) {
            // Process ToggleButton annotations (main modules)
            ToggleButton toggleAnnotation = field.getAnnotation(ToggleButton.class);
            if (toggleAnnotation != null) {
                String key = toggleAnnotation.key();
                moduleFields.putIfAbsent(key, new ArrayList<>());
                moduleFields.get(key).add(field);
            }

            // Process other annotations (sub-settings)
            processAnnotation(field, CheckBox.class, moduleFields);
            processAnnotation(field, Slider.class, moduleFields);
            processAnnotation(field, com.aftertime.ratallofyou.UI.newui.annotations.KeyBindInput.class, moduleFields);
            processAnnotation(field, com.aftertime.ratallofyou.UI.newui.annotations.ColorPicker.class, moduleFields);
            processAnnotation(field, com.aftertime.ratallofyou.UI.newui.annotations.DropdownBox.class, moduleFields);
            // Add other annotation types as needed
        }

        // Create UI panels from annotated fields
        for (Map.Entry<String, List<Field>> entry : moduleFields.entrySet()) {
            String key = entry.getKey();
            List<Field> fieldsForModule = entry.getValue();

            // Find the main toggle button for this module
            Field toggleField = fieldsForModule.stream()
                    .filter(f -> f.getAnnotation(ToggleButton.class) != null)
                    .findFirst()
                    .orElse(null);

            if (toggleField != null) {
                ToggleButton toggleAnnotation = toggleField.getAnnotation(ToggleButton.class);
                String categoryName = toggleAnnotation.category();

                // Get or create category panel
                CategoryPanel category = categories.get(categoryName);
                if (category == null) {
                    category = new CategoryPanel(categoryName, 0, 0, 200, 400);
                    categories.put(categoryName, category);
                }

                // Create module panel
                try {
                    toggleField.setAccessible(true);
                    boolean initialValue = toggleField.getBoolean(null);
                    ModulePanel modulePanel = new ModulePanel(
                            toggleAnnotation.name(),
                            toggleAnnotation.description(),
                            0, 0, 180,
                            initialValue
                    );

                    // Bind toggle to field
                    modulePanel.getToggleButton().setOnToggle(() -> {
                        try {
                            toggleField.setBoolean(null, modulePanel.isEnabled());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });

                    // Add sub-settings
                    for (Field subField : fieldsForModule) {
                        if (subField == toggleField) continue;
                        addSubSetting(modulePanel, subField);
                    }

                    category.addModule(modulePanel);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return categories;
    }

    private static <T extends java.lang.annotation.Annotation>
    void processAnnotation(Field field, Class<T> annotationClass, Map<String, List<Field>> moduleFields) {
        T annotation = field.getAnnotation(annotationClass);
        if (annotation != null) {
            try {
                // Get the key method from the annotation
                String key = (String) annotation.getClass().getMethod("key").invoke(annotation);
                moduleFields.putIfAbsent(key, new ArrayList<>());
                moduleFields.get(key).add(field);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void addSubSetting(ModulePanel modulePanel, Field field) {
        try {
            field.setAccessible(true);

            // Handle different annotation types
            CheckBox checkBox = field.getAnnotation(CheckBox.class);
            if (checkBox != null) {
                boolean current = field.getBoolean(null);
                modulePanel.addCheckBox(checkBox.title(), current, () -> {
                    try {
                        // Toggle boolean value in config
                        boolean now = field.getBoolean(null);
                        field.setBoolean(null, !now);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            Slider slider = field.getAnnotation(Slider.class);
            if (slider != null) {
                Object obj = field.get(null);
                float current;
                if (obj instanceof Number) {
                    current = ((Number) obj).floatValue();
                } else {
                    try { current = Float.parseFloat(String.valueOf(obj)); }
                    catch (Exception e) { current = slider.min(); }
                }
                String label = prettifyName(field.getName());
                modulePanel.addSlider(label, slider.min(), slider.max(), current, (val) -> {
                    try {
                        Class<?> type = field.getType();
                        if (type == int.class || type == Integer.class) {
                            field.setInt(null, Math.round(val));
                        } else if (type == float.class || type == Float.class) {
                            field.setFloat(null, val);
                        } else if (type == double.class || type == Double.class) {
                            field.setDouble(null, val);
                        } else if (type == long.class || type == Long.class) {
                            field.setLong(null, (long) val.floatValue());
                        } else {
                            // Fallback: store as int
                            field.set(null, Math.round(val));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            com.aftertime.ratallofyou.UI.newui.annotations.KeyBindInput keyBind = field.getAnnotation(com.aftertime.ratallofyou.UI.newui.annotations.KeyBindInput.class);
            if (keyBind != null) {
                String current = String.valueOf(field.get(null));
                com.aftertime.ratallofyou.UI.newui.elements.KeyBindInput element = modulePanel.addKeyBindInputReturn(keyBind.title(), current, null);
                // Attach callback after creation to avoid forward reference issues
                element.setOnChange(() -> {
                    try {
                        if (field.getType() == String.class) {
                            field.set(null, element.getKeyName());
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            com.aftertime.ratallofyou.UI.newui.annotations.ColorPicker colorPicker = field.getAnnotation(com.aftertime.ratallofyou.UI.newui.annotations.ColorPicker.class);
            if (colorPicker != null) {
                int current = field.getInt(null);
                java.awt.Color initialColor = new java.awt.Color(current, true);
                modulePanel.addColorPicker(colorPicker.title(), initialColor, () -> {
                    try {
                        // For demo, just set to black (real implementation would read from the UI element)
                        field.setInt(null, initialColor.getRGB());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            com.aftertime.ratallofyou.UI.newui.annotations.DropdownBox dropdown = field.getAnnotation(com.aftertime.ratallofyou.UI.newui.annotations.DropdownBox.class);
            if (dropdown != null) {
                String[] optionsArr = dropdown.options();
                String[] options = (optionsArr == null || optionsArr.length == 0) ? new String[]{""} : optionsArr;
                int current = field.getInt(null);
                modulePanel.addDropdown(dropdown.title(), options, current, () -> {
                    try {
                        // For demo, just set to 0 (real implementation would read from the UI element)
                        field.setInt(null, 0);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            // Add handling for other annotation types...

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static String prettifyName(String raw) {
        // Convert camelCase or snake_case to Title Case
        String spaced = raw.replace('_', ' ')
                .replaceAll("(?<!^)([A-Z])", " $1");
        String[] parts = spaced.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1).toLowerCase());
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}
