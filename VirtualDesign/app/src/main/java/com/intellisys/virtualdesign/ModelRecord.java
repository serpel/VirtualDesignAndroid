package com.intellisys.virtualdesign;

public class ModelRecord {
    private String ImageUrl;
    private String Name;
    private String ModelUrl;
    private String Category;

    public ModelRecord(){
    }

    public ModelRecord(String name, String imageUrl, String modelUrl, String category) {
        this.ImageUrl = imageUrl;
        this.ModelUrl = modelUrl;
        this.Category = category;
        this.Name = name;
    }

    public String getName() {
        return Name;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public String getModelUrl() {
        return ModelUrl;
    }

    public String getCategory() {
        return Category;
    }
}
