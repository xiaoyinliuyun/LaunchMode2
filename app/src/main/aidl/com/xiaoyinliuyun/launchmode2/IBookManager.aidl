package com.xiaoyinliuyun.launchmode2;

parcelable Book;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
}