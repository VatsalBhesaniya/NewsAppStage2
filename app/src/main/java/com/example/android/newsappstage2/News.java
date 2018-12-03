package com.example.android.newsappstage2;

public class News {
    private String mImageUrl;
    private String mSectionName;
    private String mNewsTitle;
    private String mNewsText;
    private String mAuthorName;
    private String mDatePublished;
    private String mWebUrl;

    public News(String imageUrl, String sectionName, String newsTitle, String newsText, String authorName, String datePublished, String webUrl) {
        mImageUrl = imageUrl;
        mSectionName = sectionName;
        mNewsTitle = newsTitle;
        mNewsText = newsText;
        mAuthorName = authorName;
        mDatePublished = datePublished;
        mWebUrl = webUrl;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getSectionName() {
        return mSectionName;
    }

    public String getNewsTitle() {
        return mNewsTitle;
    }

    public String getNewsText() {
        return mNewsText;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getDatePublished() {
        return mDatePublished;
    }

    public String getmWebUrl() {
        return mWebUrl;
    }
}
