CREATE table orders
(
    id                 BIGSERIAL    not null primary key,
    isbn               varchar(255) not null,
    book_title         varchar(255) not null,
    price              float8       not null,
    quantity           int          not null,
    status             varchar(255) not null,

    created_date       timestamp    not null,
    created_by         varchar(255) not null,
    last_modified_date timestamp    not null,
    last_modified_by   varchar(255) not null,
    version            int          not null
)