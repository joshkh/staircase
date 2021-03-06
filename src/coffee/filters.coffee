define (require) ->

  ng = require 'angular'
  L = require 'lodash'
  require 'services'
  pluralize = require 'pluralize'

  Filters = ng.module('steps.filters', ['steps.services'])

  Filters.filter('interpolate', ['version', (v) -> (t) -> String(t).replace(/\%VERSION\%/mg, v)])

  Filters.filter 'count', -> (xs) -> xs?.length or null

  Filters.filter 'values', -> (obj) -> L.values obj

  Filters.filter 'uniq', -> (things) -> L.uniq things

  Filters.filter 'pluralize', -> (thing, n) -> pluralize thing, n

  Filters.filter 'pluralizeWithNum', -> (thing, n) -> pluralize thing, n, true

  Filters.filter 'mappingToArray', -> (obj) ->
    return obj unless obj instanceof Object
    (Object.defineProperty v, '$key', {__proto__: null, value: k} for k, v of obj)

  Filters.filter 'roughDate', Array '$filter', (filters) -> (str) ->
    date = new Date str
    now = new Date()

    minutesAgo = (now.getTime() - date.getTime()) / 60 / 1000

    if minutesAgo < 1
      return "a moment ago"

    if minutesAgo < 2
      return "one minute ago"
    
    if minutesAgo < 60
      return "today, #{ minutesAgo.toFixed() } minutes ago"

    hoursAgo = minutesAgo / 60

    if hoursAgo < now.getHours()
      return "today, #{ hoursAgo.toFixed() } hours ago"

    daysAgo = hoursAgo / 24

    if hoursAgo > (1 + (now.getHours() / 24))
      return filters('date')(str)
    else
      return "yesterday"

  return Filters
