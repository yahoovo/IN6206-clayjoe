package com.clayjoe.backend.config;

import com.clayjoe.backend.entity.Product;
import com.clayjoe.backend.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataConfig {

    @Bean
    public CommandLineRunner demoData(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Product product = new Product("小熊猫陶瓷杯");
                repository.save(product);
            }

            repository.findAll().forEach(product -> {
                System.out.println(
                        "商品编号：" + product.getId()
                                + "，商品名称：" + product.getName()
                );
            });
        };
    }
}