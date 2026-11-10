create table if not exists prices (
    id         bigint generated always as identity primary key,
    brand_id   bigint         not null,
    product_id bigint         not null,
    price_list bigint         not null,
    priority   integer        not null,
    start_date timestamp      not null,
    end_date   timestamp      not null,
    price      numeric(19, 4) not null,
    curr       char(3)        not null
);

create index if not exists prices_brand_id_product_id on prices (brand_id, product_id);
