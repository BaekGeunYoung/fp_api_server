package com.example.fp_api_server.exception

import kotlin.reflect.KClass

class EntityNotFoundException (clazz: KClass<*>, id: Long): RuntimeException("entity not found: ${clazz.simpleName}, $id")