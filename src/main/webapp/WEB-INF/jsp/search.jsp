<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Поиск и просмотр ОГРН</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
          integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
</head>
<body>
<div class="container">

    <nav class="navbar navbar-dark bg-dark">
        <a class="navbar-brand" href="#">Приложение ТРРП</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <div>
                <ul class="navbar-nav mr-auto">
                    <li class="nav-item active">
                        <a class="nav-link" href="/search">Поиск и просмотр ОГРН<span
                                class="sr-only">(current)</span></a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/index">Добавить ОГРН</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <div style="background-color: #eee; padding: 16px; margin-top: 10px">


        <form:form method="post" action="/search" modelAttribute="egrulUser">
            <div class="input-group mb-3">
                <div class="input-group-prepend">
                    <span class="input-group-text">ОГРН</span>
                </div>
                <form:input path="ogrn" class="form-control" pattern="[0-9]{13}" placeholder="13 цифр без пробелов"
                            aria-label="" aria-describedby="basic-addon1"/>
                <div class="input-group-append">
                    <button type="submit" value="Submit" class="btn btn-outline-secondary">Найти</button>
                </div>
            </div>
        </form:form>

        <c:if test="${printData and found}">
            <span>Результаты поиска:</span>
            <dl class="row">
                <dt class="col-sm-3">ОГРН:</dt>
                <dd class="col-sm-9">${egrulUser.ogrn}</dd>

                <dt class="col-sm-3">Имя:</dt>
                <dd class="col-sm-9">${egrulUser.firstName}</dd>

                <dt class="col-sm-3">Фамилия:</dt>
                <dd class="col-sm-9">${egrulUser.lastName}</dd>

                <dt class="col-sm-3">Отчество:</dt>
                <dd class="col-sm-9">${egrulUser.patronymic}</dd>

                <dt class="col-sm-3">Основная деястельность:</dt>
                <c:forEach items="${egrulUser.egrulInfoDTOS}" var="info">
                    <c:if test="${info.isMain}">
                        <dd class="col-sm-9">${info.activityCode} ${info.description}</dd>
                    </c:if>
                </c:forEach>
            </dl>

            <span>Дополнительные деятельности:</span>
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th scope="col">Код</th>
                    <th scope="col">Наименование</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${egrulUser.egrulInfoDTOS}" var="info">
                    <tr>
                        <td>${info.activityCode}</td>
                        <td>${info.description}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>

        <c:if test="${printData and !found}">
            <p class="h3">В системе нет данных по указанному ОГРН!</p>
        </c:if>
    </div>

</div>
<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js"
        integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN"
        crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"
        integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"
        crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"
        integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl"
        crossorigin="anonymous"></script>
</body>

</html>