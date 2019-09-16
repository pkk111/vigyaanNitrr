from django.db import models

# Create your models here.
class Model(models.Model):
    title       = models.TextField()
    discription = models.TextField()