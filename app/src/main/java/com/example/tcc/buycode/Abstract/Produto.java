package com.example.tcc.buycode.Abstract;

/**
 * Created by Maykel on 31/05/2016.
 */
public class Produto {

    private String barcode;
    private String nome;
    private String categoria;
    private String desc;
    private String img_path;
    private Double preço;

    public Produto(String bc, String nome, String cat, String desc, String img, Double preco){
        this.barcode = bc;
        this.nome = nome;
        this.categoria = cat;
        this.desc = desc;
        this.img_path = img;
        this.preço = preco;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public Double getPreço() {
        return preço;
    }

    public void setPreço(Double preço) {
        this.preço = preço;
    }



}
