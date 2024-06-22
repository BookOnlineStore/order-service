CREATE table orders
(
    id                 uuid DEFAULT gen_random_uuid() primary key,
    total_price        float8       null,
    status             varchar(255) not null,
    full_name          varchar(255) not null,
    email              varchar(255) not null,
    phone_number       varchar(255) not null,
    city               varchar(255) not null,
    zip_code           varchar(255) not null,
    address            varchar(255) not null,
    created_date       timestamp    not null,
    created_by         varchar(255),
    last_modified_date timestamp    not null,
    last_modified_by   varchar(255),
    version            int          not null
);

CREATE table line_items
(
    id        bigserial primary key,
    order_id  uuid         not null,
    isbn      VARCHAR(255) not null,
    title     VARCHAR(255) not null,
    author    VARCHAR(255) not null,
    publisher VARCHAR(255) not null,
    supplier  VARCHAR(255) not null,
    price     float8       not null,
    photos    varchar(255)[],
    quantity  int          not null,
    version   int          not null
);